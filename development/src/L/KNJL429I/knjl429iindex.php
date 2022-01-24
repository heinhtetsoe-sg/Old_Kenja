<?php

require_once('knjl429iModel.inc');
require_once('knjl429iQuery.inc');

class knjl429iController extends Controller {
    var $ModelClassName = "knjl429iModel";
    var $ProgramID      = "KNJL429I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                    $this->callView("knjl429iForm1");
                    break 2;
                case "knjl429i":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjl429iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl429iForm1");
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
$knjl429iCtl = new knjl429iController;
?>
