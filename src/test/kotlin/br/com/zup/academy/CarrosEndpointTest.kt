package br.com.zup.academy

import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CarrosEndpointTest(
    val repository: CarrosRepository,
    val grpcClient: CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub,
){

    @BeforeEach
    fun setUp(){
        //preparando cenario
        repository.deleteAll()
    }

    @Test
    fun `deve adicionar um novo carro`(){

        //cenario -> preparar o banco ou onde o teste for executar a ação
        // (geralmente fica na notação @BeforeEach)

        //ação
        val response = grpcClient.adicionar(CarrosRequest.newBuilder()
            .setModelo("Gol")
            .setPlaca("EDO-0011")
            .build())

        //validação

        with(response){
            Assertions.assertNotNull(id)
            Assertions.assertTrue(repository.existsById(id)) // efeito colateral (verificar no banco)
        }
    }

    @Test
    fun `nao deve adicionar novo carro quando carro existente`(){

        //cenario
        val oCarro = repository.save(Carros("Gol","EDO-0001"))

        //ação
        val oErro = assertThrows<StatusRuntimeException>{
            grpcClient.adicionar(CarrosRequest.newBuilder()
                .setPlaca(oCarro.placa)
                .setModelo(oCarro.modelo)
                .build())
        }

        //validação
        with(oErro){
            Assertions.assertEquals(Status.ALREADY_EXISTS.code,  this.status.code)
            Assertions.assertEquals("placa ja cadastrada",this.status.description)
        }
    }

    @Test
    fun `nao deve aficionar novo carro quando parametros invalidos`(){

        //cenario -> beforeEach

        //ação
        val oErro = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(CarrosRequest.newBuilder().build())
        }

        //validação
        with(oErro){
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            Assertions.assertEquals("dados de entrada invalido", this.status.description)
        }
    }

    @Factory
    class Clients{
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub?{
            return CarrosGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}