<?php

require_once('for_php7.php');

require_once('knjg100Model.inc');
require_once('knjg100Query.inc');

class knjg100Controller extends Controller {
    var $ModelClassName = "knjg100Model";
    var $ProgramID      = "KNJG100";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjg100":                                //メニュー画面もしくはSUBMITした場合
                case "change_class":                        //クラス変更時のSUBMITした場合
                    $sessionInstance->knjg100Model();      //コントロールマスタの呼び出し
                    $this->callView("knjg100Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjg100Ctl = new knjg100Controller;
var_dump($_REQUEST);
?>
