<?php

require_once('for_php7.php');

require_once('knjl510gModel.inc');
require_once('knjl510gQuery.inc');

class knjl510gController extends Controller {
    var $ModelClassName = "knjl510gModel";
    var $ProgramID      = "KNJL510G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl510g":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl510gModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl510gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl510gCtl = new knjl510gController;
//var_dump($_REQUEST);
?>
