package com.shediz.score.service;

import com.shediz.score.model.*;
import com.shediz.score.repository.CommentRepository;
import com.shediz.score.repository.LikeRepository;
import com.shediz.score.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MainService
{
    private final PostRepository postRepository;

    private final LikeRepository likeRepository;

    private final CommentRepository commentRepository;

    @Autowired
    public MainService(PostRepository post, LikeRepository likeRepository, CommentRepository commentRepository)
    {
        this.postRepository = post;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
    }

    public String getPostOwner(String pid)
    {
        return postRepository.findById(pid).map(Post::getOwner).orElse(null);
    }

    public String addPost(String ownerUserName, String postId)
    {
        return postRepository.save(new Post(ownerUserName, postId)).getPid();
    }

    //For delete account
    @Transactional
    public void deleteAllUserData(String userName)
    {
        postRepository.findAllByOwner(userName)
                .stream()
                .map(Post::getPid)
                .forEach(this::deletePost);
    }

    @Transactional
    public void deletePost(String pid)
    {
        likeRepository.deleteAllByPostId(pid);
        commentRepository.deleteAllByPostId(pid);
        postRepository.deleteById(pid);
    }

    public boolean addLike(String userName, String postId)
    {
        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isPresent())
        {
            Post post = postOptional.get();
            post.addLike(new Like(userName, postId));
            postRepository.save(post);

            return true;
        }

        return false;
    }

    public Optional<Like> getLike(String pid, String ownerUserName)
    {
        return likeRepository.findById(new LikeId(ownerUserName, pid));
    }

    public List<LikeWrapper> getLikes(List<String> pidList, String userName)
    {
        ArrayList<LikeWrapper> likeWrappers = new ArrayList<>();

        for (String pid: pidList)
        {
            Long numPostLikes = likeRepository.countByPostId(pid);

            Optional<Like> optionalLike = getLike(pid, userName);
            boolean isUserLikedPost = optionalLike.isPresent();

            likeWrappers.add(new LikeWrapper(pid, numPostLikes, isUserLikedPost));
        }

        return likeWrappers;
    }

    public void deleteLike(Like like)
    {
        likeRepository.delete(like);
    }

    public Long addComment(String userName, String postId, String text, boolean isNegative)
    {
        Comment comment = new Comment();
        comment.setUsername(userName);
        comment.setPostId(postId);
        comment.setText(text);
        comment.setIsNegative(isNegative);


        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isPresent())
        {
            Post post = postOptional.get();
            post.addComment(comment);
            return postRepository.save(post).getLastCommentId();
        }

        return null;
    }

    public Comment getComment(Long cid)
    {
        return commentRepository.findById(cid).orElse(null);
    }

    public void deleteComment(Comment comment)
    {
        commentRepository.delete(comment);
    }

    public Long countPostLike(String pid)
    {
        return likeRepository.countByPostId(pid);
    }

    public List<Comment> getComments(String pid, int page, int size)
    {
        return commentRepository.findAllByPostId(pid, PageRequest.of(page, size));
    }

    public Long countPostScore(String pid)
    {
        return countPostLike(pid) +
        commentRepository.countByPostIdAndIsNegativeFalse(pid);
    }
}
