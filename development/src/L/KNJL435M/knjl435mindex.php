<?php

require_once('for_php7.php');

require_once('knjl435mModel.inc');
require_once('knjl435mQuery.inc');

class knjl435mController extends Controller
{
    public $ModelClassName = "knjl435mModel";
    public $ProgramID      = "KNJL435M";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);

        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":
                    // CSVダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knja435mForm1");
                    }
                    $sessionInstance->setAccessLogDetail("EO", $ProgramID);
                    break 2;
                case "":
                case "knjl435m":
                    $sessionInstance->knjl435mModel();
                    $this->callView("knjl435mForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl435mCtl = new knjl435mController();
