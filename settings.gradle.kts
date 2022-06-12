/*
 *    Copyright 2021 Gabrielle Guimarães de Oliveira
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

rootProject.name = "andesite"

include("andesite-shared")
include("andesite-world:andesite-world-common")
include("andesite-world:andesite-world-anvil")
include("andesite-world:andesite-world-slime")
include("andesite-protocol:andesite-protocol-common")
include("andesite-protocol:andesite-protocol-java")
include("andesite-protocol:andesite-protocol-java:andesite-protocol-java-v756")
include("andesite-protocol:andesite-protocol-bedrock")
include("andesite-protocol:andesite-protocol-bedrock:andesite-protocol-bedrock-v465")
include("andesite-item:andesite-item-generator")
include("andesite-item")
include("andesite-komanda")
include("andesite-server:andesite-server-common")
include("andesite-server:andesite-server-bedrock")
include("andesite-server:andesite-server-java")
