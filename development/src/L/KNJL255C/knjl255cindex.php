<?php

require_once('for_php7.php');

require_once('knjl255cModel.inc');
require_once('knjl255cQuery.inc');

class knjl255cController extends Controller {
    var $ModelClassName = "knjl255cModel";
    var $ProgramID      = "KNJL255C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl255c":								//メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
					$sessionInstance->knjl255cModel();		//コントロールマスタの呼び出し
                    $this->callView("knjl255cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjl255cCtl = new knjl255cController;
var_dump($_REQUEST);
?>
