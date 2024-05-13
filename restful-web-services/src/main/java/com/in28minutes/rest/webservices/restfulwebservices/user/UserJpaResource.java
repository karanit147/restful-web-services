package com.in28minutes.rest.webservices.restfulwebservices.user;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.in28minutes.rest.webservices.restfulwebservices.jpa.PostRepository;
import com.in28minutes.rest.webservices.restfulwebservices.jpa.UserRepository;

import jakarta.persistence.Id;
import jakarta.persistence.PostRemove;
import jakarta.validation.Valid;


@RestController
public class UserJpaResource {

	private UserDaoService service;
	
	private UserRepository userRepository; 
	
	private PostRepository postRepository;
	
	public UserJpaResource(UserDaoService service, UserRepository userRepository, PostRepository postRepository) {
		this.service = service;
		this.userRepository = userRepository;
		this.postRepository = postRepository;
	}
	
	//Get /users
	@GetMapping("/jpa/users")
	public List<User> retrieveAllUsers() {
		return userRepository.findAll();
	}
	
	// EntityModel
	// WebMvcLinkBuilder
	
	@GetMapping("/jpa/users/{id}")
	// this is hateos method of retrieval
	public EntityModel<User> retrieveUserByIdHateos(@PathVariable int id) { 
		Optional<User> user = userRepository.findById(id);
		

		if(user.isEmpty()) {
			throw new UserNotFoundException("id: "+id);
		}
		
		EntityModel<User> entityModel = EntityModel.of(user.get());
		
		WebMvcLinkBuilder link = linkTo(methodOn(this.getClass()).retrieveAllUsers());
		entityModel.add(link.withRel("all-users"));
		
		return entityModel;
	}
	
 
//	@GetMapping("/jpa/users/{id}")
//	public Optional<User> retrieveUserById(@PathVariable int id) { 
//		Optional<User> user = repository.findById(id);
//		
//		if(user.isEmpty()) {
//			throw new UserNotFoundException("id: "+id);
//		}
//		
//		return user;
//	}
	
	@PostMapping("/jpa/users")
	public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
		User savedUser = userRepository.save(user);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedUser.getId()).toUri();
		return ResponseEntity.created(location).build();
	}
	
	@DeleteMapping("/jpa/users/{id}")
	public void deleteUser(@PathVariable int id) { 
		userRepository.deleteById(id);

	}
	
	@GetMapping("/jpa/users/{id}/posts")
	public List<Post> retrievePostsForUser(@PathVariable int id) { 
		Optional<User> user = userRepository.findById(id);
		
		if(user.isEmpty()) {
			throw new UserNotFoundException("id: "+id);
		}

		return user.get().getPosts();
		
	}
	
	@PostMapping("/jpa/users/{id}/posts")
	public ResponseEntity<Object> createPostsForUser(@PathVariable int id, @Valid @RequestBody Post post) { 
		Optional<User> user = userRepository.findById(id);
		
		if(user.isEmpty()) {
			throw new UserNotFoundException("id: "+id);
		}

		post.setUser(user.get());
		
		Post savedPost = postRepository.save(post);
		
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedPost.getId()).toUri();
		return ResponseEntity.created(location).build();
	
	}
	
//	http://localhost:8080/jpa/users/10001/posts/1
	
	@GetMapping("jpa/users/{user_id}/posts/{post_id}")
	public Optional<Post> retirevePostForUserWithPostId(@PathVariable int user_id, @PathVariable int post_id) {
		Optional<User> user = userRepository.findById(user_id);
		
		if(user.isEmpty()) {
			throw new UserNotFoundException("id: "+user_id);
		}
		
		Optional<Post> post = user.get().getPosts().stream().filter(p -> p.getId() == post_id).findFirst();
		
		if(post.isEmpty()) {
			throw new UserNotFoundException("Post_Id: "+post_id+" is not found with User id: "+user_id);
		}
		
		System.out.println(post);
		
		return post;
		
		
	}
	
	
	
	  
	
}
