<?php
require_once('knjl351kModel.inc');
require_once('knjl351kQuery.inc');

class knjl351kController extends Controller {
    var $ModelClassName = "knjl351kModel";
    var $ProgramID      = "KNJL351K";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl351k":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl351kModel();       //コントロールマスタの呼び出し
                    $this->callView("knjl351kForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjl351kCtl = new knjl351kController;
//var_dump($_REQUEST);
?>
