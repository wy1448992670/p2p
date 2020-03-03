package jobs;

import java.util.List;

import utils.tsign.TSign;
import utils.tsign.eSign.SignHelper;

/**
 * 测试签章使用
 * 
 * @ClassName SignJobTest
 * @Description TODO(这里用一句话描述这个类的作用)
 * @author zj
 * @Date 2018年9月10日 上午10:31:20
 * @version 1.0.0
 */
public class SignJobTest {
	public static void main(String[] args) {
		SignHelper.initProject();
		String investPdfPath = "D:\\workspace\\yiyilc_qykh\\yiyilc_sp2p\\public\\pact\\J112353\\2018-09-08-J112351-001.doc.pdf";
		String bString = "D:\\workspace\\yiyilc_qykh\\yiyilc_sp2p\\public\\pact\\J112353";
		List<String> signServiceIds = TSign.doSignWithTemplateSealByStream(investPdfPath, bString,
				"2018-09-08-J112351-001.doc".concat(".sign.pdf"), null);

	}
}
