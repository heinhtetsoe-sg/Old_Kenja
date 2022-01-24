<?php

require_once('for_php7.php');

require_once('knjm490nModel.inc');
require_once('knjm490nQuery.inc');

class knjm490nController extends Controller {
    var $ModelClassName = "knjm490nModel";
    var $ProgramID      = "KNJM490N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm490n":                              //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm490nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm490nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm490nCtl = new knjm490nController;
//var_dump($_REQUEST);
?>
