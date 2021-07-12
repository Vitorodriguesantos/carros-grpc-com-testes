package br.com.zup.academy

import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CarrosEndpoint(@Inject val repository: CarrosRepository):  CarrosGrpcServiceGrpc .CarrosGrpcServiceImplBase(){


    override fun adicionar(request: CarrosRequest?, responseObserver: StreamObserver<CarroResponse>?) {

        if(repository.existsByPlaca(request?.placa)){
            responseObserver?.onError(Status.ALREADY_EXISTS
                .withDescription("placa ja cadastrada")
                .asRuntimeException())

            return
        }

        val carro = Carros(request!!.modelo, request!!.placa)

        try {
            repository.save(carro)
        }catch (e: ConstraintViolationException){
            responseObserver?.onError(Status.INVALID_ARGUMENT
                .withDescription("dados de entrada invalido")
                .asRuntimeException())
            return
        }

        responseObserver?.onNext(CarroResponse.newBuilder().setId(carro.id!!).build())
        responseObserver?.onCompleted()
    }
}

