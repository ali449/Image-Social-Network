package com.shediz.gateway.service;

import com.shediz.gateway.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserDao
{
    private final UserRepository userRepository;

    private final FollowRepository followRepository;

    private final RequestRepository requestRepository;

    @Autowired
    UserDao(UserRepository userRepository, FollowRepository followRepository,
            RequestRepository requestRepository)
    {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.requestRepository = requestRepository;
    }

    public void removeUser(User user)
    {
        followRepository.deleteAllFollow(user.getUsername());

        userRepository.delete(user);
    }

    public List<User> getUserContains(String str)
    {
        return userRepository.findTop10ByUsernameContainingIgnoreCase(str);
    }

    public Optional<User> getUser(String username)
    {
        return Optional.ofNullable(userRepository.findOneByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("User Name " + username + " Not Found!"));
    }

    public List<User> getMultiUser(List<String> userNames)
    {
        return userRepository.findByUsernameIn(userNames);
    }

    public void update(String userName, String bio, boolean isPrivate)
    {
        User user = userRepository.getOne(userName);
        user.setBio(bio);
        user.setIsPrivate(isPrivate);
        userRepository.save(user);
    }

    public boolean isFollowing(String fromUserName, String toUserName)
    {
        Optional<Follow> follow = followRepository.findByFid_FkFromAndFid_FkTo(fromUserName, toUserName);

        return follow.isPresent();
    }

    public Long getNumFollowing(String userName)
    {
        return followRepository.countByFid_FkFrom(userName);
    }

    public Long getNumFollowers(String userName)
    {
        return followRepository.countByFid_FkTo(userName);
    }

    public void addFollowRequest(String requestedUser, String targetUser)
    {
        requestRepository.save(new Request(requestedUser, targetUser));
    }

    //@Transactional
    public void acceptFollow(String sourceUserName, String targetUserName)
    {
        FollowId id = new FollowId(sourceUserName, targetUserName);

        requestRepository.deleteById(id);
        followRepository.save(new Follow(id));
    }

    public void rejectFollow(String sourceUserName, String targetUserName)
    {
        requestRepository.deleteById(new FollowId(sourceUserName, targetUserName));
    }

    public boolean addFollowing(String requestedUser, String targetUser)
    {
        followRepository.save(new Follow(requestedUser, targetUser));
        return true;
    }

    public void removeFollowing(String requestedUser, String targetUser)
    {
        followRepository.delete(new Follow(requestedUser, targetUser));
    }

    public List<String> getFollowing(String userName, int page, int size)
    {
        List<Follow> followList = followRepository.findByFid_FkFrom(userName, PageRequest.of(page, size));

        return followList.stream()
                .map(Follow::getFkTo)
                .collect(Collectors.toList());
    }

    public List<String> getFollowers(String userName, int page, int size)
    {
        List<Follow> followersList = followRepository.findByFid_FkTo(userName, PageRequest.of(page, size));

        return followersList.stream()
                .map(Follow::getFkTo)
                .collect(Collectors.toList());
    }
}
