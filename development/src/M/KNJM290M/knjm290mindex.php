<?php

require_once('for_php7.php');

require_once('knjm290mModel.inc');
require_once('knjm290mQuery.inc');

class knjm290mController extends Controller {
    var $ModelClassName = "knjm290mModel";
    var $ProgramID      = "KNJM290M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjm290m":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjm290mModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm290mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm290mCtl = new knjm290mController;
//var_dump($_REQUEST);
?>

