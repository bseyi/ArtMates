package com.example.artmates.fragments;

import com.example.artmates.Post;

public class ParcelableObject {
    Post post;

    public ParcelableObject(){}

    public void setPost(Post post){
        this.post = post;
    }
    public Post getPost(){
        return post;
    }
}
