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

import at.favre.tools.rocketexporter.dto.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface RocketChatService {
    @POST("/api/v1/login")
    Call<LoginResponseDto> login(@Body LoginDto request);

    @GET("/api/v1/me")
    Call<LoginResponseDto> tokenAuth(@HeaderMap Map<String, String> headers);

    @GET("/api/v1/groups.list?count=0&offset=0")
    Call<RocketChatGroups> getAllGroups(@HeaderMap Map<String, String> header);

    @GET("/api/v1/channels.list?count=0&offset=0")
    Call<RocketChatChannel> getAllChannels(@HeaderMap Map<String, String> header);

    @GET("/api/v1/im.list?count=0&offset=0")
    Call<RocketChatDm> getAllDirectMessages(@HeaderMap Map<String, String> header);

    @GET("/api/v1/groups.history")
    Call<RocketChatMessageWrapperDto> getAllMessagesFromGroup(@HeaderMap Map<String, String> header, @Query("roomId") String groupId, @Query("offset") long offset, @Query("count") long count);

    @GET("/api/v1/channels.history")
    Call<RocketChatMessageWrapperDto> getAllMessagesFromChannels(@HeaderMap Map<String, String> header, @Query("roomId") String channelId, @Query("offset") long offset, @Query("count") long count);

    @GET("/api/v1/im.history")
    Call<RocketChatMessageWrapperDto> getAllMessagesFromDirectMessages(@HeaderMap Map<String, String> header, @Query("roomId") String dmId, @Query("offset") long offset, @Query("count") long count);
}
