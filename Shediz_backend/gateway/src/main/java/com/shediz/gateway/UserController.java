package com.shediz.gateway;

import com.shediz.gateway.database.User;
import com.shediz.gateway.database.UserWithFollowStatus;
import com.shediz.gateway.exception.InvalidFileException;
import com.shediz.gateway.messaging.FollowMessage;
import com.shediz.gateway.messaging.GeneralMessage;
import com.shediz.gateway.messaging.Sender;
import com.shediz.gateway.service.FileStorageService;
import com.shediz.gateway.service.UserAuthService;
import com.shediz.gateway.service.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public final class UserController
{
    private static final int PAGE_SIZE = 10;

    private final UserAuthService userAuthService;

    private final UserDao userDao;

    private FileStorageService fileService;

    private Sender publisher;

    private static String GATEWAY_SECRET;

    @Value("${gateway-secret}")
    public void setGatewaySecret(String gatewaySecret)
    {
        GATEWAY_SECRET = gatewaySecret;
    }

    @Autowired
    public UserController(UserAuthService userAuthService, UserDao userDao)
    {
        this.userAuthService = userAuthService;
        this.userDao = userDao;
    }

    @Lazy
    @Autowired
    public void setFileService(final FileStorageService fileService)
    {
        this.fileService = fileService;
    }

    @Lazy
    @Autowired
    public void setPublisher(final Sender sender)
    {
        this.publisher = sender;
    }

    @PostMapping(value = "/up_pic_profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> uploadProfilePic(@AuthenticationPrincipal User user, @RequestParam("file") MultipartFile file)
    {
        if (file.isEmpty())
            return ResponseEntity.badRequest().build();

        String extension = Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1];
        if (!(Objects.equals(file.getContentType(), MediaType.IMAGE_JPEG_VALUE) && extension.equals("jpg")))
        {
            throw new InvalidFileException("Only image/jpeg allowed");
        }

        fileService.storeFile(file, user.getUsername() + ".jpg");

        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @GetMapping(value = "/s/pic_profile/{username}", produces = MediaType.IMAGE_JPEG_VALUE)
    ResponseEntity<Resource> downloadProfilePic(@PathVariable String username)
    {
        Resource resource = fileService.loadFileAsResource(username + ".jpg");

        /*String contentType = null;
        try
        {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        if (contentType == null)
            contentType = "application/octet-stream";*/

        return ResponseEntity.ok()
                //.contentType(MediaType.parseMediaType(contentType))
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PutMapping("/request_follow_result/{username}")
    ResponseEntity<?> requestFollowResult(@AuthenticationPrincipal User current, @PathVariable String username,
                                          @RequestParam("has_accept") boolean hasAccept)
    {
        Optional<User> optional = userDao.getUser(username);
        if (optional.isPresent())
        {
            if (hasAccept)
            {
                userDao.acceptFollow(username, current.getUsername());

                //Notify to recommender service
                publisher.sendMessageToQueue1(new GeneralMessage(GeneralMessage.EventType.USER_FOLLOWED,
                        username, current.getUsername(), null, null));
            }
            else
               userDao.rejectFollow(username, current.getUsername());

            return ResponseEntity.ok(Collections.singletonMap("success", true));
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping("/request_follow/{username}")
    ResponseEntity<?> requestFollow(@AuthenticationPrincipal User current, @PathVariable String username)
    {
        if (current.getUsername().equals(username))
            return ResponseEntity.ok(true);

        Optional<User> optional = userDao.getUser(username);
        if (optional.isPresent())
        {
            User targetUser = optional.get();

            if (targetUser.getIsPrivate())
            {
                userDao.addFollowRequest(current.getUsername(), targetUser.getUsername());

                //Send push notification to user
                publisher.sendMessageToQueue3(
                        new FollowMessage(current.getUsername(), targetUser.getUsername()));

                return ResponseEntity.accepted().body(Collections.singletonMap("success", false));
            }

            boolean result = userDao.addFollowing(current.getUsername(), targetUser.getUsername());

            //Notify to recommender service
            publisher.sendMessageToQueue1(new GeneralMessage(GeneralMessage.EventType.USER_FOLLOWED,
                    current.getUsername(), username, null, null));

            return ResponseEntity.ok(Collections.singletonMap("success", result));
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/request_unfollow/{username}")
    ResponseEntity<?> requestUnFollow(@AuthenticationPrincipal User current, @PathVariable String username)
    {
        Optional<User> optional = userDao.getUser(username);
        if (optional.isPresent())
        {
            User targetUser = optional.get();

            userDao.removeFollowing(current.getUsername(), targetUser.getUsername());

            //Notify to recommender service
            publisher.sendMessageToQueue2(new GeneralMessage(GeneralMessage.EventType.USER_UNFOLLOWED,
                    current.getUsername(), username, null, null));

            return ResponseEntity.ok(Collections.singletonMap("success", true));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/current")
    User getCurrent(@AuthenticationPrincipal final User user)
    {
        user.setNumFollowing(userDao.getNumFollowing(user.getUsername()));
        user.setNumFollowers(userDao.getNumFollowers(user.getUsername()));
        return user;
    }

    @GetMapping("/s/user/{username}")
    ResponseEntity<?> getUserInfo(@PathVariable String username,
                     @RequestParam(value = "needFullInfo", required = false) boolean needFullInfo)
    {
        Optional<User> optional = userDao.getUser(username);

        if (optional.isPresent())
        {
            User requestedUser = optional.get();

            if (needFullInfo)
            {
                requestedUser.setNumFollowing(userDao.getNumFollowing(requestedUser.getUsername()));

                requestedUser.setNumFollowers(userDao.getNumFollowers(requestedUser.getUsername()));
            }

            return ResponseEntity.ok(requestedUser);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/s/search_user")
    ResponseEntity<?> searchUsers(@RequestParam String q)
    {
        if (q.trim().isEmpty())
            return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(userDao.getUserContains(q));
    }

    //For use in other microservices
    //return whole user following
    @GetMapping(value = "/s/multi_user")
    ResponseEntity<?> getMultiUserInfo(@RequestParam List<String> userNames)
    {
        if (userNames.isEmpty())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return ResponseEntity.ok(userDao.getMultiUser(userNames));
    }

    //For other microservices
    //return list of usernames
    @GetMapping("/s/following/{username}")
    ResponseEntity<?> getFollowingUserNames(@PathVariable String username, HttpServletRequest request)
    {
        String secret = request.getHeader("Secret");

        if (isBlank(secret) || !secret.equals(GATEWAY_SECRET))
            return ResponseEntity.badRequest().body("Error in Gateway");

        return ResponseEntity.ok(userDao.getFollowing(username, 0, Integer.MAX_VALUE));
    }

    @GetMapping("/following/{username}")
    List<String> getFollowing(@PathVariable String username, @RequestParam int page)
    {
        return userDao.getFollowing(username, page, PAGE_SIZE);
    }

    @GetMapping("/followers/{username}")
    List<String> getFollowers(@PathVariable String username, @RequestParam int page)
    {
        return userDao.getFollowers(username, page, PAGE_SIZE);
    }

    @GetMapping("/user_with_follow/{targetUserName}")
    ResponseEntity<?> getUserWithFollowStatus(@AuthenticationPrincipal final User user,
                                              @PathVariable String targetUserName)
    {
        Optional<User> optional = userDao.getUser(targetUserName);

        if (optional.isPresent())
        {
            User requestedUser = optional.get();

            requestedUser.setNumFollowing(userDao.getNumFollowing(requestedUser.getUsername()));

            requestedUser.setNumFollowers(userDao.getNumFollowers(requestedUser.getUsername()));

            boolean isFollowing = userDao.isFollowing(user.getUsername(), targetUserName);

            return ResponseEntity.ok(new UserWithFollowStatus(requestedUser, isFollowing));
        }

        return ResponseEntity.notFound().build();
    }

    //For other microservices authorization
    @GetMapping("/s/can_get/{fromUserName}/{toUserName}")
    boolean isUserAccessGetAnother(@PathVariable String fromUserName, @PathVariable String toUserName)
    {
        Optional<User> targetUser = userDao.getUser(toUserName);
        if (targetUser.isPresent())
        {
            User owner = targetUser.get();

            if (!owner.getIsPrivate() || owner.getUsername().equals(fromUserName))
                return true;

            return userDao.isFollowing(fromUserName, toUserName);
        }
        return false;
    }

    @PutMapping("/edit_user")
    ResponseEntity<?> editUser(@AuthenticationPrincipal final User user, @RequestParam("bio") String userBio,
                               @RequestParam("is_private") boolean isPrivate)
    {
        userDao.update(user.getUsername(), userBio, isPrivate);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/register")
    ResponseEntity<?> register(@RequestParam("username") String username,
                               @RequestParam("password") String password, @RequestParam("bio") String bio,
                               @RequestParam(name = "is_private", required = false) boolean isPrivate)
    {
        if (isBlank(username) || isBlank(password)  || password.length() < 4 ||
                !username.matches("[A-Za-z0-9_]{4,}") || username.equals("data"))
            return ResponseEntity.badRequest().build();

        userAuthService.register(username, password, bio, isPrivate);

        //Notify to recommender service
        publisher.sendMessageToQueue1(new GeneralMessage(GeneralMessage.EventType.USER_CREATED,
                username, null, null, null));

        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @DeleteMapping("/delete_account")
    ResponseEntity<?> deleteAccount(@AuthenticationPrincipal final User user, @RequestParam final String username,
                          @RequestParam final String password)
    {
        if (userAuthService.matchAllCredential(user.getToken(), username, password))
        {
            userDao.removeUser(user);

            GeneralMessage deleteMessage = new GeneralMessage(GeneralMessage.EventType.USER_DELETED,
                    user.getUsername(), null, null, null);
            publisher.sendMessageToQueue1(deleteMessage);
            publisher.sendMessageToQueue2(deleteMessage);

            return ResponseEntity.ok(Collections.singletonMap("success", true));
        }
        return ResponseEntity.ok(Collections.singletonMap("success", false));
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> login(@RequestParam("username") final String username,
                            @RequestParam("password") final String password)
    {
        Optional<String> result = userAuthService.login(username, password);

        return result.map(token -> ResponseEntity.ok(Collections.singletonMap("token", token)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/logout")
    ResponseEntity<?> logout(@AuthenticationPrincipal final User user)
    {
        userAuthService.logout(user);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    private static boolean isBlank(String str)
    {
        return str == null || str.isEmpty();
    }
}
