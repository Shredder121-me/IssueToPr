package com.github.shredder121.issuetopr

import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.TestRestTemplate
import org.springframework.web.client.RestTemplate

import com.github.shredder121.gh_event_api.handler.create.CreateHandler
import com.github.shredder121.gh_event_api.GHEventApiServer

class Application {

    @Autowired RestTemplate restTemplate

    @Bean
    CreateHandler convertIssueToPr() {
        { payload ->
            def ref = payload.ref
            def match = ref =~ $/(?:gh-|i)(\d+)/$
            if(payload.refType == 'branch' && match) {
                def repo = payload.repository
                def issue = match.group 1
                restTemplate.postForObject(
                    "https://api.github.com/repos/$repo.fullName/pulls",
                    [issue: issue, head: ref, base: payload.masterBranch],
                    Object
                )
            }
        } as CreateHandler
    }

    @Bean
    RestTemplate restTemplate(Environment env) {
        def user = env.getRequiredProperty('github_user')
        def oauth = env.getRequiredProperty('github_oauth')
        def bean = new TestRestTemplate(user, oauth)
    }
}

GHEventApiServer.start Application
