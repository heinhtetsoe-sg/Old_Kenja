<?php

require_once('for_php7.php');

require_once('knjl031fModel.inc');
require_once('knjl031fQuery.inc');

class knjl031fController extends Controller {
    var $ModelClassName = "knjl031fModel";
    var $ProgramID      = "KNJL031F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl031f":						    //メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjl031fModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl031fForm1");
                    exit;
				case "csv":     //CSVダウンロード
					if (!$sessionInstance->getDownloadModel()) {
						$this->callView("knjl031fForm1");
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
$knjl031fCtl = new knjl031fController;
?>
