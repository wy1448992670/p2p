import business.RedPackageHistory;
import business.User;
import business.UserBankAccounts;
import constants.Constants;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;
import utils.ErrorInfo;
import utils.test.BankNumberUtil;
import utils.test.IdCardGenerator;
import utils.test.RandomValueUtil;

import java.util.Date;

public class ApplicationTest extends FunctionalTest {

    @Test
    public void testThatIndexPageWorks() {
        Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }


    /**
     * fsdfssssssss
     */
    @Test
    public void regiteUserTest(){


        /*全局查找  创建测试用户修改  按相应提示处理即可*/


        for(int i=0;i<50;i++) {

            String phone = RandomValueUtil.getTelephone();

            User user = new User();
            user.time = new Date();
            user.mobile = phone;
            user.name = RandomValueUtil.getChineseName();
            user.password = "qqqqqqqq";

//        if (isMobileRegister) {
//            user.mobile = mobile;
//            user.isMobileVerified = true;
//        }

            user.email = RandomValueUtil.getEmail(6, 12);
//        user.recommendUserName = recoName;


            ErrorInfo error = new ErrorInfo();
            user.register(Constants.CLIENT_PC, error);


            /*
             * 激活用户邮箱
             */
            user.activeEmail(error);

            System.out.println("用户编号：" + user.getId());

            //实名认证
            IdCardGenerator idCardGenerator = new IdCardGenerator();
            user.updateCertification(user.name, idCardGenerator.generate(), user.id, error);

            RedPackageHistory.sendAuthRed(user);

            //添加银行卡
            UserBankAccounts bankUser = new UserBankAccounts();
            bankUser.userId = user.getId();
            bankUser.bankName = "中国工商银行";
            bankUser.bankCode = "102";
            //		bankUser.provinceCode = addProviceCode;
            //		bankUser.cityCode = addCityCode;
            //		bankUser.branchBankName = addBranchBankName;
            //		bankUser.province = provice;
            //		bankUser.city = city;
            bankUser.account = BankNumberUtil.getBrankNumber("2");
            bankUser.accountName = user.name;
            bankUser.mobile = phone;

            bankUser.addUserBankAccount(error, false);

        }

    }

}