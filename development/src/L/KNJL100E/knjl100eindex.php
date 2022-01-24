<?php

require_once('for_php7.php');

require_once('knjl100eModel.inc');
require_once('knjl100eQuery.inc');

class knjl100eController extends Controller
{
    public $ModelClassName = "knjl100eModel";
    public $ProgramID      = "KNJL100E";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //CSV出力
                case "csv":
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl100eForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl100eForm1");
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
$knjl100eCtl = new knjl100eController();
?>
