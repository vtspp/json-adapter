package com.br.vtspp.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public final class JsonAdapter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Map<Object, Object> readValues;

    private JsonAdapter() {
        throw new RuntimeException("A classe não deve ser instanciada. Utilize o padrão builder para a criação de uma nova instância.");
    }

    private JsonAdapter(final Map<Object, Object> readValues) {
        this.readValues = readValues;
    }

    /**
     * @param json Parâmetro que será usado para converter em um mapa
     * @param target Key usada para identificar qual o dado que será adaptado.
     *               Caso o item não seja identificado, nenhum dado será alterado e o mesmo json será retornado.
     * @param function Função usada para alterar o dados identificado no target.
     * @return Retorna um novo json com o dado alterado. Em casos de erro ou caso não seja identificado, o mesmo json de origem será retornado.
     */
    public static String adapterSingleValue(String json, final String target, final Function<String, String> function) {
        try {
            var map = MAPPER.readValue(json, Map.class);

            var replaceValueExecutor = ReplaceValue.createNewInstance(target, map)
                    .replaceByFunction(function);

            if (replaceValueExecutor.wasReplaced()) {
                return MAPPER.writeValueAsString(map);
            }

            return json;
        } catch (IOException e) {
            log.warn("Ocorreu um erro ao tentar converter o JSON {}. Nenhuma informação será alterada e será usado o mesmo JSON de origem.", json, e);
            return json;
        }
    }

    /**
     * @param json Parâmetro em formato de json que será usado para popular os dados do mapa contido na instância
     * @return Retorna uma instância da classe JsonAdapter. Com ela é possível modificar mais de um dado recebido no arquivo json
     * @throws IOException
     */
    public static JsonAdapter createJsonAdapterBuild(String json) throws IOException {
        final var readValues = MAPPER.readValue(json, Map.class);
        return new JsonAdapter(readValues);
    }

    /**
     * @param target Key utilizada para recuperar o valor dentro do mapa
     * @param function Parâmetro em forma de função. Pode ser utilizado como argumento a função disponível na classe JsonAdapterCR ou
     *                 uma outra função personalizada conforme a necessidade.
     *
     * @return Retorna a instância de um JsonAdapterCR, tornando flexivél para ser chamado multiplas vezes a fim de modificar outros parâmetros contidos no mapa.
     * Basta fornecer uma nova function a cada chamada.
     */
    public JsonAdapter adapterByFunction(final String target, final Function<Object, Object> function) {
        ReplaceValue.createNewInstance(target, this.readValues)
                .replaceByFunction(function);

        return this;
    }

    /**
     * @return Transforma todos os valores armazenados na instância do mapa em um novo json
     * @throws JsonProcessingException Uma exception será lançada caso algum erro ocorra. Precisa ser tratado por que utilizar.
     */
    public String buildToJson() throws JsonProcessingException {
        return MAPPER.writeValueAsString(this.readValues);
    }

    /**
     * @param maxSize Parâmetro usado para definir a quantidade máxima de caracteres
     * @return Retorna uma Function que modifica a string conforme a quantidade de caracteres recebida como parâmetro
     */
    public static Function<String, String> functionMaxSizeAccepted(int maxSize) {
        final var initPosition = 0;
        final var endPosition = Math.max(maxSize, 1);
        return o -> o.substring(initPosition, endPosition);
    }
}
