## Contribuindo

  Criar uma ligação remota com o repositório principal: `git remote add upstream git@github.com:@master/Projeto_IA369.git`;
- Atualize seu repositório local: `git fetch upstream`;
- Fazer a ligação devramo com repositório principal: `git checkout upstream/dev -b dev`;
- Crie o seu ramo de funcionalidade com base em dev: `git checkout -b my-new-feature`;
## Corrida
Instale módulos locais:
console `` 
npm i
`` 

Em seguida, você pode iniciar o servidor da web e executar testes com o seguinte comando:
console `` 
npm run test: assistir
``
- Escreva testes para seu recurso;
- Quando estiver pronto, continue com os próximos passos:

- Assegure-se de que o código não tenha erros de lint;
- Se algum erro foi exibido, execute npm run formatpara corrigi-lo;
- Adicione suas alterações`git add .:`;
- Confirmar as alterações:` git commit -m 'Add some feature'`;
- Empurre para o ramo`git push origin my-new-feature:@master`: D
- Envie uma solicitação pull para devbranch e convoque 
- Executando testes uma vez
- Apenas execute `npm test`.

## Dicas
- Envie apenas um recurso para solicitação pull;
- Envie pequenos pedidos de pull;
- Escreva testes para todos os recursos.
## Construa etapas
> Se você estiver enviando uma solicitação pull, não precisará executar esses comandos.

Com a última versão de código na devramificação, serão executados os seguintes passos:

Na devramificação, basta executar este comando:

versão npm <versão>
Quando <version>pode ser `patch`, `minor` ou `major`.

E feito ;)
