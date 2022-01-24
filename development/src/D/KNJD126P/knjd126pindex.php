<?php

require_once('for_php7.php');

require_once('knjd126pModel.inc');
require_once('knjd126pQuery.inc');

class knjd126pController extends Controller
{
    public $ModelClassName = "knjd126pModel";
    public $ProgramID      = "KNJD126P";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "form1":
                case "select1":
                case "reset":
                case "conversion":
                    $this->callView("knjd126pForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("form1");
                    break 1;
                case "form2":
                case "select2":
                case "form2_reset":
                case "form2_conversion":
                    $this->callView("knjd126pForm2");
                    break 2;
                case "form2_update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("form2");
                    break 1;
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
$knjd126pCtl = new knjd126pController();
