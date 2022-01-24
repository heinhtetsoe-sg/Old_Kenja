<?php

require_once('for_php7.php');

require_once('knjm300mModel.inc');
require_once('knjm300mQuery.inc');

class knjm300mController extends Controller {
    var $ModelClassName = "knjm300mModel";
    var $ProgramID      = "KNJM300M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm300m":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm300mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm300mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm300mCtl = new knjm300mController;
//var_dump($_REQUEST);
?>

