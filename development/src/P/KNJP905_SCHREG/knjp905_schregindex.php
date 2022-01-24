<?php

require_once('for_php7.php');

require_once('knjp905_schregModel.inc');
require_once('knjp905_schregQuery.inc');

class knjp905_schregController extends Controller
{
    public $ModelClassName = "knjp905_schregModel";
    public $ProgramID      = "KNJP905_SCHREG";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "chgBudgetSDiv":
                case "main":
                case "exec_main":
                case "knjp905_schreg":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp905_schregModel();        //コントロールマスタの呼び出し
                    $this->callView("knjp905_schregForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "exec"://CSV取込
                    $sessionInstance->getExecModel();
                    $sessionInstance->setCmd("exec_main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjp905_schregCtl = new knjp905_schregController;
//var_dump($_REQUEST);
?>

