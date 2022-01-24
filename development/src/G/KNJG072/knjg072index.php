<?php

require_once('for_php7.php');

require_once('knjg072Model.inc');
require_once('knjg072Query.inc');

class knjg072Controller extends Controller {
    var $ModelClassName = "knjg072Model";
    var $ProgramID      = "KNJG072";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg072":                                //メニュー画面もしくはSUBMITした場合
                case "print":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjg072Model();      //コントロールマスタの呼び出し
                    $this->callView("knjg072Form1");
                    exit;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("print");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjg072Ctl = new knjg072Controller;
var_dump($_REQUEST);
?>
