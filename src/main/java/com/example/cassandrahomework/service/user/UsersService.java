package com.example.cassandrahomework.service.user;

import com.example.cassandrahomework.entity.UserEntity;
import com.example.cassandrahomework.mapper.UserMapper;
import com.example.cassandrahomework.model.user.AuthenticationCredentials;
import com.example.cassandrahomework.model.user.User;
import com.example.cassandrahomework.model.user.UserId;
import com.example.cassandrahomework.model.user.exception.EmailConflictException;
import com.example.cassandrahomework.model.user.exception.UserAuthenticationException;
import com.example.cassandrahomework.model.user.exception.UserNotFoundException;
import com.example.cassandrahomework.repository.user.JpaUsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsersService {
    private final JpaUsersRepository usersRepository;
    private static final Logger LOG = LoggerFactory.getLogger(UsersService.class);

    public UsersService(JpaUsersRepository userRepository) {
        this.usersRepository = userRepository;
    }

    public User findById(UserId userId){
        LOG.debug("Method findById called");
        Optional<UserEntity> optionalUser= usersRepository.findById(userId.getValue());
        if (optionalUser.isEmpty()){
            throw new UserNotFoundException("User is not found with id = " + userId.getValue());
        }
        return UserMapper.toUser(optionalUser.get());
    }

    public User register(User user){
        LOG.debug("Method register called");
        Optional<UserEntity> optionalUser = usersRepository.findByEmail(user.email());
        if (optionalUser.isPresent()){
            throw new EmailConflictException("Email is already used");
        }
        UserEntity userEntity = UserMapper.toUserEntity(user);

        return UserMapper.toUser(usersRepository.save(userEntity));
    }

    @Transactional
    public void update(UserId userId, String email, String username){
        LOG.debug("Method update called");
        Optional<UserEntity> optionalUser = usersRepository.findById(userId.getValue());
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User is not found with id = " + userId.getValue());
        }
        Optional<UserEntity> optionalUserEntity = usersRepository.findByEmailAndNotId(userId.getValue(), email);
        if (optionalUserEntity.isPresent()){
            throw new EmailConflictException("Email " + email + " is already taken");
        }
        UserEntity userEntity = optionalUser.get();
        userEntity.setEmail(email);
        userEntity.setUsername(username);
        usersRepository.save(userEntity);
    }
    public void delete(final UserId userId){
        LOG.debug("Method delete called");
        Optional<UserEntity> optionalUser = usersRepository.findById(userId.getValue());
        if (optionalUser.isEmpty()){
            throw new UserNotFoundException("User is not found with id = " + userId.getValue());
        }
        usersRepository.deleteById(userId.getValue());
    }
    public Optional<UserId> authenticate(AuthenticationCredentials credentials){
        LOG.debug("Method authenticate called");
        Optional<UserEntity> optionalUser = usersRepository.findByEmailAndPassword(credentials.email(), credentials.password());
        if (optionalUser.isEmpty()){
            throw new UserAuthenticationException("User not authenticated by email = "+credentials.email()+" and password = "+credentials.password());
        }
        return Optional.of(new UserId(optionalUser.get().getId()));
    }

    public void changePassword(UserId userId, String newPassword){
        LOG.debug("Method changePassword called");
        Optional<UserEntity> optionalUser = usersRepository.findById(userId.getValue());
        if (optionalUser.isEmpty()){
            throw new UserNotFoundException("User is not found with id = " + userId.getValue());
        }
        UserEntity userEntity = optionalUser.get();
        userEntity.setPassword(newPassword);
        usersRepository.save(userEntity);
    }
}
