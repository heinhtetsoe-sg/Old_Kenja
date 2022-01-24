<?php

require_once('for_php7.php');

require_once('knjl140kModel.inc');
require_once('knjl140kQuery.inc');

class knjl140kController extends Controller
{
    public $ModelClassName = "knjl140kModel";
    public $ProgramID      = "KNJL140K";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl140kForm1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl140kForm1");
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
$knjl140kCtl = new knjl140kController;
