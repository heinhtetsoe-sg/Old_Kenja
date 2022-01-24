<?php

require_once('for_php7.php');

require_once('knjm020mModel.inc');
require_once('knjm020mQuery.inc');

class knjm020mController extends Controller {
    var $ModelClassName = "knjm020mModel";
    var $ProgramID      = "knjm020m";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm020m":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm020mModel();       //コントロールマスタの呼び出し
                    $this->callView("knjm020mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm020mCtl = new knjm020mController;
//var_dump($_REQUEST);
?>
