<?php

require_once('for_php7.php');

require_once('knjm432wModel.inc');
require_once('knjm432wQuery.inc');

class knjm432wController extends Controller
{
    public $ModelClassName = "knjm432wModel";
    public $ProgramID      = "KNJM432W";     //プログラムID

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "main":
                case "change":              //科目（講座）が変わったとき
                case "change_order":        //出力順が変わったとき
                case "reset":
                case "read":
                case "pre":
                case "next":
                case "updPrint":
                case "updPrint2":
                    $sessionInstance->getMainModel();
                    $this->callView("knjm432wForm1");
                    break 2;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                case "updatePrint":
                    $sessionInstance->setAccessLogDetail("U", $ProgramID);
                    $sessionInstance->getUpdatePrintModel();
                    //変更済みの場合は詳細画面に戻る
                    $sessionInstance->setCmd("updPrint");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm432wCtl = new knjm432wController();
