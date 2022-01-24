<?php

require_once('for_php7.php');

require_once('knjl330fModel.inc');
require_once('knjl330fQuery.inc');

class knjl330fController extends Controller {
    var $ModelClassName = "knjl330fModel";
    var $ProgramID      = "KNJL330F";

    function main() {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjl330fApplicantdiv":
                case "knjl330fTestdiv":
                case "knjl330f":                            //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjl330fModel();        //コントロールマスタの呼び出し
                    $this->callView("knjl330fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl330fCtl = new knjl330fController;
?>
