<?php

require_once('knjl380iModel.inc');
require_once('knjl380iQuery.inc');

class knjl380iController extends Controller {
    var $ModelClassName = "knjl380iModel";
    var $ProgramID      = "KNJL380I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl380iForm1");
                    break 2;
                case "knjl380i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjl380iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl380iForm1");
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
$knjl380iCtl = new knjl380iController;
?>
