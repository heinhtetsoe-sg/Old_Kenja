<?php

require_once('for_php7.php');

require_once('knjm330mModel.inc');
require_once('knjm330mQuery.inc');

class knjm330mController extends Controller {
    var $ModelClassName = "knjm330mModel";
    var $ProgramID      = "knjm330m";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm330m":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm330mModel();       //コントロールマスタの呼び出し
                    $this->callView("knjm330mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm330mCtl = new knjm330mController;
//var_dump($_REQUEST);
?>
