<?php

require_once('for_php7.php');

require_once('knjd105iModel.inc');
require_once('knjd105iQuery.inc');

class knjd105iController extends Controller {
    var $ModelClassName = "knjd105iModel";
    var $ProgramID      = "KNJD105I";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd105i":                                //メニュー画面もしくはSUBMITした場合
                case "knjd105iupdated":                         //メニュー画面もしくはSUBMITした場合
                case "edit":                                //メニュー画面もしくはSUBMITした場合
                case "change_grade":
                    $sessionInstance->knjd105iModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd105iForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();        //コントロールマスタの呼び出し
                    $sessionInstance->setCmd("knjd105iupdated");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd105iCtl = new knjd105iController;
//var_dump($_REQUEST);
?>
