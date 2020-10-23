package com.shediz.post;

import com.shediz.post.messaging.GeneralMessage;
import com.shediz.post.messaging.Sender;
import com.shediz.post.model.*;
import com.shediz.post.service.FileStorageService;
import com.shediz.post.service.PostService;
import com.shediz.post.utils.Util;
import com.shediz.spamdetect.SpamFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.shediz.post.utils.Util.*;

@RestController
public class PostController
{
    public static final int PAGE_SIZE = 10;

    private final PostService postService;

    private final Util utilService;

    private final SpamFilter spamDetect;

    private FileStorageService fileService;

    private final Sender publisher;

    private enum ResourceType
    {
        POST, IMAGE
    }

    @Autowired
    public PostController(PostService postService, Util util, Sender sender, SpamFilter spamDetect)
    {
        this.postService = postService;
        this.utilService = util;
        this.publisher = sender;
        this.spamDetect = spamDetect;
        spamDetect.loadModel();
    }

    @Lazy
    @Autowired
    void setFileService(final FileStorageService fileService)
    {
        this.fileService = fileService;
    }

    @GetMapping("/s/suggest_tags")
    Mono<ResponseEntity<List<String>>> suggestTags(@RequestParam("t") String t, @RequestParam int page)
    {
        int from = page * PAGE_SIZE;

        return postService.suggestTag(t, from, PAGE_SIZE)
                .map(ResponseEntity::ok).switchIfEmpty(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping("/s/tags/{tag}")
    Mono<ResponseEntity<List<Post>>> searchInTagsPost(@PathVariable String tag, @RequestParam int page)
    {
        int from = page * PAGE_SIZE;

        if (!tag.startsWith("#"))
            tag = "#" + tag;


        return postService.searchTagAllPosts(tag, from, PAGE_SIZE).map(posts ->
        {
            posts.removeIf(Post::getIsPrivate);

            return ResponseEntity.ok(posts);
        }).switchIfEmpty(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping("/s/search_post")
    Mono<ResponseEntity<List<Post>>> searchInContentPosts(@RequestParam("q") String query, @RequestParam int page)
    {
        int from = page * PAGE_SIZE;

        return postService.searchInContentAllPosts(query, from, PAGE_SIZE).map(posts ->
        {
            posts.removeIf(Post::getIsPrivate);

            return ResponseEntity.ok(posts);
        }).switchIfEmpty(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping("/new_posts")
    Mono<ResponseEntity<List<Post>>> getNewPosts(@RequestParam int page, HttpServletRequest request)
    {
        if (isNotFromGateWay(request))
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build());

        String sourceUserName = request.getHeader("UserName");

        int from = page * PAGE_SIZE;

        return utilService.getUserFollowingNamesNonBlocking(sourceUserName)
                .flatMap(followingNames ->
                        postService.getByUserNamesSorted(followingNames, from, PAGE_SIZE)
                                .map(ResponseEntity::ok)
                                .switchIfEmpty(Mono.just(ResponseEntity.noContent().build())))
                .switchIfEmpty(Mono.just(ResponseEntity.noContent().build()));

        //Blocking way
        /*String[] followingNames = getUserFollowingNamesBlocking(sourceUserName);

        if (followingNames == null || followingNames.length == 0)
            return Mono.just(ResponseEntity.noContent().build());

        return postService.getByUserNamesSorted(followingNames, 0, 10)
                .map(ResponseEntity::ok).switchIfEmpty(Mono.just(ResponseEntity.noContent().build()));*/
    }

    @GetMapping("/user/{username}")
    Mono<ResponseEntity<?>> findUserPosts(@PathVariable String username, @RequestParam int page,
                                          HttpServletRequest request)
    {
        if (isNotFromGateWay(request))
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Only GateWay can send this request"));

        String sourceUserName = request.getHeader("UserName");
        if (isEmpty(sourceUserName))
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("User Name not found in header"));


        return utilService.checkAccessGetUser(sourceUserName, username).flatMap(hasAccess ->
        {
           if (hasAccess)
           {
               int from = page * PAGE_SIZE;
               return postService.getByUserName(username, from, PAGE_SIZE)
                       .map(ResponseEntity::ok);
           }

           return Mono.just(ResponseEntity
                   .status(HttpStatus.FORBIDDEN)
                   .body("User " + sourceUserName + " must follow " + username));
        }).switchIfEmpty(Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Unable to fetch " + username)));
    }

    //Blocking way
    /*@GetMapping("/s/user/{username}")
    public ResponseEntity<?> findUserPosts(
            @PathVariable String username, @RequestParam("from") int from, @RequestParam("size") int size)
    {
        if (from < 0 || size < 0 || (size - from) > MAX_PAGE_ITEMS)
            return new ResponseEntity<>("from and size Must be > 0 And (size-from) < 10", HttpStatus.BAD_REQUEST);


        User targetUser = getUserInfo(username);
        if (targetUser == null)
            return new ResponseEntity<>("Unable to fetch " + username, HttpStatus.SERVICE_UNAVAILABLE);


        if (targetUser.getIsPrivate())
            return new ResponseEntity<>("User Page " + targetUser.getUsername() + " is Private ",
                    HttpStatus.FORBIDDEN);

        //Public User
        return new ResponseEntity<>(postService.getByUserName(username, from, size), HttpStatus.OK);
    }*/

    /**
     * This method only used when recommender service returns some post ids to client.
     * @param ids List<String> requested posts id from recommender service (client fetched this ids)
     * @return List<Post> requested public posts
     */
    @GetMapping(value = "/s/ids", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<?>> findPostsByIds_1(@RequestBody IdsWrapper ids)
    {
        if (ids == null || ids.getIds().isEmpty() || ids.getIds().size() > PAGE_SIZE)
            return Mono.just(ResponseEntity.badRequest().body("Required ids size between 1 and 10"));

        return postService.getByIds(ids.getIds()).map(posts ->
        {
            posts.removeIf(Post::getIsPrivate);

            return ResponseEntity.ok(posts);
        });
    }

    //PutMapping for some clients doesn't support request body on get method
    @PutMapping(value = "/s/ids", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<?>> findPostsByIds_2(@RequestBody IdsWrapper ids)
    {
        return findPostsByIds_1(ids);
    }

    /**
     * @param id of post
     * @param isPrivateRequest Request is Public or Private? true->private, false->public
     * @return Image or Post
     */
    private Mono<ResponseEntity<?>> authAndReturn(String id, HttpServletRequest request, boolean isPrivateRequest)
    {
        return authAndReturn(id, request, isPrivateRequest, ResourceType.POST, false);
    }

    /**
     * @param id of post
     * @param isPrivateRequest Request is Public or Private? true->private, false->public
     * @param returnType Image or Post? true -> Post, false -> Image
     * @param needOriginalImage If Request Type is Image, get original or thumb?
     * @return Image or Post
     */
    private Mono<ResponseEntity<?>> authAndReturn(String id, HttpServletRequest request, boolean isPrivateRequest,
                                           ResourceType returnType, boolean needOriginalImage)
    {
        String sourceUserName = request.getHeader("UserName");

        if (isPrivateRequest)
        {
            if (isNotFromGateWay(request))
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Only GateWay can send this request"));

            if (isEmpty(sourceUserName))
                return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User Name not found in header"));
        }


        return postService.getById(id).flatMap(post -> utilService.checkAccessGetPost(sourceUserName, post)
                .flatMap(hasAccess ->
                {
                    if (hasAccess)
                    {
                        if (returnType == ResourceType.POST)
                            return Mono.just(ResponseEntity.ok(post));
                        else if (returnType == ResourceType.IMAGE)
                        {
                            Resource resource = fileService.loadFileAsResource(post.getUsername(),
                                    post.getId(), ".jpg", needOriginalImage);
                            return Mono.just(ResponseEntity.ok()
                                    .contentType(MediaType.IMAGE_JPEG)
                                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                                            resource.getFilename() + "\"")
                                    .body(resource));
                       }
                       return Mono.just(ResponseEntity
                               .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                               .body("Media Type Error: " + returnType));
                   }//if hasAccess
                   return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied"));
                })
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Unable to fetch " + post.getUsername() + " from user service"))))
            .switchIfEmpty(Mono.just(ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No " + id + " found!")));//for first flatMap
    }

    @GetMapping("/s/thumb_image/{id}")
    Mono<ResponseEntity<?>> getPublicThumbImagePost(@PathVariable String id, HttpServletRequest request)
    {
        return authAndReturn(id, request, false, ResourceType.IMAGE, false);
    }

    @GetMapping("/s/main_image/{id}")
    Mono<ResponseEntity<?>> getPublicMainImagePost(@PathVariable String id, HttpServletRequest request)
    {
        return authAndReturn(id, request, false, ResourceType.IMAGE, true);
    }

    @GetMapping("/thumb_image/{id}")
    Mono<ResponseEntity<?>> getThumbImagePost(@PathVariable String id, HttpServletRequest request)
    {
        return authAndReturn(id, request, true, ResourceType.IMAGE, false);
    }

    @GetMapping("/main_image/{id}")
    Mono<ResponseEntity<?>> getMainImagePost(@PathVariable String id, HttpServletRequest request)
    {
        return authAndReturn(id, request, true, ResourceType.IMAGE, true);
    }

    /**
     * @param id of post
     * @return Post
     */
    @GetMapping("/s/{id}")
    Mono<ResponseEntity<?>> findPublicPostById(@PathVariable String id, HttpServletRequest request)
    {
        return authAndReturn(id, request, false);
    }

    /**
     * @param id of post
     * @return Post
     */
    @GetMapping("/{id}")
    Mono<ResponseEntity<?>> findPostById(@PathVariable String id, HttpServletRequest request)
    {
        return authAndReturn(id, request, true);
    }

    /**
     * @return id of new post
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Mono<? extends ResponseEntity<?>> addPost(@ModelAttribute PostWrapper postWrapper, HttpServletRequest request)
    {
        MultipartFile file = postWrapper.getFile();

        String content = postWrapper.getContent();

        String userName = request.getHeader("UserName");

        if (file == null || file.isEmpty() || isNotFromGateWay(request) || isEmpty(userName))
            return Mono.just(ResponseEntity.badRequest().body("Cannot process " + content));


        String extension = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];
        if (!(Objects.equals(file.getContentType(), MediaType.IMAGE_JPEG_VALUE) && extension.equals("jpg")))
            return Mono.just(ResponseEntity.badRequest().body("Only Image/JPEG Allowed"));

        boolean isPrivateUser = false;
        if (request.getHeader("IsPrivate") != null)
            isPrivateUser = Boolean.parseBoolean(request.getHeader("IsPrivate"));

        Post post = new Post();
        post.setContent(content);
        post.setUsername(userName);
        post.setIsPrivate(isPrivateUser);
        post.setDate(new MyDate());
        post.setIsSpam(spamDetect.isSpamText(content));

        if (content != null)
            post.setTags(fetchHashTags(content));
        else
            post.setTags(new ArrayList<>());


        boolean finalIsPrivateUser = isPrivateUser;
        return postService.save(post)
                .flatMap(saveId ->
                {
                    //File Name is Equals to Post Id
                    String fileName = fileService.storeFile(file, post.getUsername(), saveId, ".jpg");

                    //Notify to score and recommender services
                    if (!finalIsPrivateUser)
                        publisher.sendMessage(new GeneralMessage(GeneralMessage.EventType.POST_CREATED,
                                userName, null, saveId, post.getTags()));

                    return Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                            .body(Collections.singletonMap("save_id", fileName)));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @DeleteMapping("/{id}")
    Mono<? extends ResponseEntity<?>> deletePostById(@PathVariable String id, HttpServletRequest request)
    {
        if (id.isEmpty())
            return Mono.just(ResponseEntity.badRequest().body("Empty ID"));

        String userName = request.getHeader("UserName");

        if (isNotFromGateWay(request) || isEmpty(userName))
            return Mono.just(ResponseEntity.badRequest().body("Cannot process request for " + id));


        return postService.deleteById(id)
                .flatMap(result ->
                {
                    fileService.deleteFile(userName, id + ".jpg");
                    fileService.deleteFile(userName, id + "_thumb.jpg");

                    //Notify to score and recommender services
                    publisher.sendMessage(new GeneralMessage(GeneralMessage.EventType.POST_DELETED,
                            userName, null, id, null));

                    return Mono.just(ResponseEntity.ok(Collections.singletonMap("success", result)));
                })
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("success", false))));
    }

    //Delete account done in Receiver class
    /*@DeleteMapping("/all/{username}") //Don't need to notify, because this called when user account deleted
    Mono<ResponseEntity<String>> deleteAllPostUser(@PathVariable String username, HttpServletRequest request)
    {
        String authUserName = request.getHeader("UserName");
        if (username.isEmpty() || isEmpty(authUserName))
            return Mono.just(ResponseEntity.badRequest()
                    .body("Wrong user names: " + username + ":" + authUserName));

        if (isNotFromGateWay(request))
            return Mono.just(ResponseEntity.badRequest().body("Only gateway can send this request"));


        return postService.deleteByUserName(username)
                .flatMap(result ->
                {
                    fileService.deleteFolder(username);

                    return Mono.just(new ResponseEntity<>(result, HttpStatus.ACCEPTED));
                })
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Result is empty")));
    }*/
}
