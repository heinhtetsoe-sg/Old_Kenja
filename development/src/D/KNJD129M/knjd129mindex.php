<?php

require_once('for_php7.php');


require_once('knjd129mModel.inc');
require_once('knjd129mQuery.inc');

class knjd129mController extends Controller
{
    public $ModelClassName = "knjd129mModel";
    public $ProgramID      = "KNJD129M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "main":
                case "sanSyutu":
                case "disp":
                case "reset":
                case "subclasscd":
                    $this->callView("knjd129mForm1");
                    break 2;
                case "chaircd":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd129mForm1");
                    break 2;
                case "calc":
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
                    $this->callView("knjd129mForm1");
                    break 2;
                case "update":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdateModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("main");
                    break 1;
                case "error":
                    $this->callView("error");
                    break 2;
                case "":
                    $this->callView("knjd129mForm1");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjd129mCtl = new knjd129mController();
