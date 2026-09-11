package com.pgis.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pgisOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("警用设备数据服务 API")
                        .description("""
                                警用设备基本信息（JYSB_SBXX_001）与定位信息（JYSB_SBXX_002）的查询、增删改查及 CSV 导入接口。
                                
                                - 导入文件前缀：`sb001*` → 基本信息，`sb002*` → 定位信息
                                - 主键冲突：覆盖更新，并在导入结果中返回覆盖主键
                                - 统一响应：`{ code, message, data }`，`code=0` 表示成功
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("PGIS")));
    }
}
