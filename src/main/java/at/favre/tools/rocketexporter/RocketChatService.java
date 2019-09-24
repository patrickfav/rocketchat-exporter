/*
 *  Copyright 2017 Patrick Favre-Bulle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.favre.tools.rocketexporter;

import at.favre.tools.rocketexporter.dto.LoginDto;
import at.favre.tools.rocketexporter.dto.LoginResponseDto;
import at.favre.tools.rocketexporter.dto.RocketChatGroups;
import at.favre.tools.rocketexporter.dto.RocketChatMessageWrapperDto;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface RocketChatService {
    @POST("/api/v1/login")
    Call<LoginResponseDto> login(@Body LoginDto request);

    @GET("/api/v1/groups.history")
    Call<RocketChatMessageWrapperDto> getAllMessagesFromGroup(@HeaderMap Map<String, String> header, @Query("roomId") String roomId, @Query("offset") long offset, @Query("count") long count);

    @GET("/api/v1/groups.list")
    Call<RocketChatGroups> getAllGroups(@HeaderMap Map<String, String> header);
}
