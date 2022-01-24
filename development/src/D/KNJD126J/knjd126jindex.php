<?php

require_once('for_php7.php');

require_once('knjd126jModel.inc');
require_once('knjd126jQuery.inc');

class knjd126jController extends Controller
{
    public $ModelClassName = "knjd126jModel";
    public $ProgramID      = "KNJD126J";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "form1":
                case "reset":
                case "conversion":
                    $this->callView("knjd126jForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    break 1;
                case "form2":
                case "reset2":
                case "form2_conversion":
                    $this->callView("knjd126jForm2");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("form1");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd126jCtl = new knjd126jController();
