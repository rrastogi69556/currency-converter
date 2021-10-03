package org.project.currencyconverter.controller;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class HomeControllerTest
{
    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("Given site is up and running," +
        " when accessed root url," +
        "then redirected to swagger-ui documentation page ")
    public void testRedirectionToSwagger() throws Exception
    {
        this.mockMvc.perform(get("/"))
            .andExpect(status().is(302))
            .andExpect(redirectedUrl("/swagger-ui/"));
    }
}
