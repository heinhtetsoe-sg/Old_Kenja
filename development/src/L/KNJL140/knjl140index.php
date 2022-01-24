<?php

require_once('for_php7.php');

require_once('knjl140Model.inc');
require_once('knjl140Query.inc');

class knjl140Controller extends Controller
{
    public $ModelClassName = "knjl140Model";
    public $ProgramID      = "KNJL140";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                case "exec":    //ＣＳＶダウンロード
                    if (!$sessionInstance->getDownloadModel()) {
                        $this->callView("knjl140Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $this->callView("knjl140Form1");
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
$knjl140Ctl = new knjl140Controller();
