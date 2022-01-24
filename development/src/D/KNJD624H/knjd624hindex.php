<?php

require_once('for_php7.php');

require_once('knjd624hModel.inc');
require_once('knjd624hQuery.inc');

class knjd624hController extends Controller {
    var $ModelClassName = "knjd624hModel";
    var $ProgramID      = "KNJD624H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd624h":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd624hModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd624hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd624hCtl = new knjd624hController;
//var_dump($_REQUEST);
?>
