<?php

require_once('for_php7.php');

require_once('knjd280cModel.inc');
require_once('knjd280cQuery.inc');

class knjd280cController extends Controller {
    var $ModelClassName = "knjd280cModel";
    var $ProgramID      = "KNJD280C";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knjd280c":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd280cModel();        //コントロールマスタの呼び出し
                    $this->callView("knjd280cForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjd280cCtl = new knjd280cController;
//var_dump($_REQUEST);
?>

