<?php

require_once('for_php7.php');


require_once('knjd129pModel.inc');
require_once('knjd129pQuery.inc');

class knjd129pController extends Controller
{
    public $ModelClassName = "knjd129pModel";
    public $ProgramID      = "KNJD129P";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "subclasscd":
                case "reset":
                    $this->callView("knjd129pForm1");
                    break 2;
                case "chaircd":
                    $this->callView("knjd129pForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd129pCtl = new knjd129pController();
