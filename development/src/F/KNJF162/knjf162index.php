<?php

require_once('for_php7.php');

require_once('knjf162Model.inc');
require_once('knjf162Query.inc');

class knjf162Controller extends Controller {
    var $ModelClassName = "knjf162Model";
    var $ProgramID      = "KNJF162";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf162":                                //メニュー画面もしくはSUBMITした場合
                case "change_class":                        //クラス変更時のSUBMITした場合
                    $sessionInstance->knjf162Model();      //コントロールマスタの呼び出し
                    $this->callView("knjf162Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjf162Ctl = new knjf162Controller;
var_dump($_REQUEST);
?>
