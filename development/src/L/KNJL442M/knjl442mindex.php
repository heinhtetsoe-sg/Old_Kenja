<?php

require_once('for_php7.php');

require_once('knjl442mModel.inc');
require_once('knjl442mQuery.inc');

class knjl442mController extends Controller
{
    public $ModelClassName = "knjl442mModel";
    public $ProgramID      = "KNJL442M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    // ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl442mForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "":
                case "knjl442m":
                    $sessionInstance->knjl442mModel();
                    $this->callView("knjl442mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl442mCtl = new knjl442mController();
