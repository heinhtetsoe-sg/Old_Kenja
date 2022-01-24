<?php

require_once('for_php7.php');

require_once('knjl315nModel.inc');
require_once('knjl315nQuery.inc');

class knjl315nController extends Controller
{
    public $ModelClassName = "knjl315nModel";
    public $ProgramID      = "KNJL315N";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl315nForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl315nForm1");
                    break 2;
                case "error":
                    $this->callView("error");
                    break 2;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjl315nCtl = new knjl315nController();
