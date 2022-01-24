<?php

require_once('for_php7.php');

require_once('knjh101bModel.inc');
require_once('knjh101bQuery.inc');

class knjh101bController extends Controller {
    var $ModelClassName = "knjh101bModel";
    var $ProgramID      = "KNJH101B";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjh101b":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjh101bModel();        //コントロールマスタの呼び出し
                    $this->callView("knjh101bForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjh101bCtl = new knjh101bController;
//var_dump($_REQUEST);
?>
