# Bichinho Virtual Afetivo 

Bichinho Virtual Afetivo é uma aplicação móvel capaz de reconhecer emoções do usuário e, a partir delas e de outros estímulos, sintetizar emoções na expressão facial do Bichinho de acordo com seu humor e personalidade.

## Funcionalidades do Bichinho Virtual Afetivo:
   * Reconhecimento de emoções:
       * Fala/Texto    
       * Face
   * Síntese de emoções: 
       * Motor de Processamento de Emoções (MPE) que utiliza as variáveis de appraisal para processamento de inputs ativos, inputs passivos e controle de humor.
   * Estímulos multimodais:
       * Localização
       * Sensor de proximidade
       * Bluetooth
       * Horário
       * Previsão do tempo
       
## Screenshots
![alt text](https://github.com/suelensilva/Projeto_IA369/blob/master/BichinhoVirtual/screenshots/screenshot-1.png "Tela principal - Bichinho Extrovertido")
![alt text](https://github.com/suelensilva/Projeto_IA369/blob/master/BichinhoVirtual/screenshots/screenshot-2.png "Tela principal - Bichinho Neurótico")

## Ferramentas e Abordagens adotadas:
  * Ferramentas:
     * [Android Studio](https://developer.android.com/studio/?hl=pt-br) - Ambiente de desenvolvimento
     * [API IBM Watson Natural Language Understanding](https://www.ibm.com/watson/services/natural-language-understanding/) - API para detectar emoções em textos
     * [API Yandex](https://tech.yandex.com/translate/) - API para traduzir textos do português para o inglês
     * [Google Mobile Vision API para Android](https://developers.google.com/vision/introduction) - API para detectar landmarks em uma imagem de face e detectar sorrisos 
     * [Toolkit OpenSMILE](https://www.audeering.com/technology/opensmile/) - Ferramenta de extração de features acústicas
     * [Weka](https://www.cs.waikato.ac.nz/ml/weka/) - Coleção de algoritmos de machine learning 
     * [Emo-DB](http://www.emodb.bilderbar.info/) - Base de dados de vozes
  * Abordagens:
     * Teorias de Appraisal: síntese de emoções baseado no modelo OCC (Ortony, A., Clore, G., & Collins, A. (1988). The Cognitive Structure of Emotions. Cambridge: Cambridge University Press. doi:10.1017/CBO9780511571299)
     * Modelo de personalidades: baseado no modelo Big Five (Goldberg, L. R. (1990). An alternative "description of personality": the big-five factor structure. Journal of personality and social psychology, 59(6), 1216.)

## Instruções de compilação
  * Instale a IDE Android Studio conforme as instruções específicas para o seu sistema operacional. Consulte a [documentação](https://developer.android.com/studio/install?hl=pt-br) oficial para maiores detalhes.
  * Faça o clone deste repositório e utilize o comando "Import Project" do Android Studio para abrir o o projeto que estará no diretório `BichinhoVirtual`.
  * Antes de começar será necessário obter uma chave de API conforme orientado na documentação da API de tradução do Yandex. Também será necessário obter um usuário e senha da API de Natural Language Understanding do IBM Watson.
  * Crie um arquivo chamado `config.properties` no diretório `BichinhoVirtual/app` e adicione o seguinte conteúdo, substituindo os valores conforme os dados obtidos na ferramentas do item anterior (observação: todos os valores devem ser informados entre aspas duplas):
    ```
    yandexKey=[YOUR_YANDEX_KEY]
    ibmNluServiceUser=[YOUR_IBM_NLU_SERVICE_USER]
    ibmNluServicePassword=[YOUR_IBM_NLU_SERVICE_PASSWORD]
    ```
  * Utilizando o emulador ou um dispositivo conectado, clique na opção `Run`do Android Studio para buildar e executar o aplicativo.
  
License
-------
Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
