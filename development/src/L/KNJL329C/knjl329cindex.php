<?php

require_once('for_php7.php');

require_once('knjl329cModel.inc');
require_once('knjl329cQuery.inc');

class knjl329cController extends Controller {
    var $ModelClassName = "knjl329cModel";
    var $ProgramID      = "KNJL329C";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl329c":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl329cModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl329cForm1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()) {
						$this->callView("knjl329cForm1");
					}
					break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjl329cCtl = new knjl329cController;
?>
