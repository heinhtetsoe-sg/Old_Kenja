<?php

require_once('for_php7.php');

require_once('knjd108vModel.inc');
require_once('knjd108vQuery.inc');

class knjd108vController extends Controller {
    var $ModelClassName = "knjd108vModel";
    var $ProgramID      = "KNJD108V";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd108v":                                //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd108vModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd108vForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd108vCtl = new knjd108vController;
//var_dump($_REQUEST);
?>
