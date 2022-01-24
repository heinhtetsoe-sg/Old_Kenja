<?php

require_once('knjl374iModel.inc');
require_once('knjl374iQuery.inc');

class knjl374iController extends Controller {
    var $ModelClassName = "knjl374iModel";
    var $ProgramID      = "KNJL374I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl374iForm1");
                    break 2;
                case "knjl374i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjl374iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl374iForm1");
                    exit;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }

        }
    }
}
$knjl374iCtl = new knjl374iController;
?>
