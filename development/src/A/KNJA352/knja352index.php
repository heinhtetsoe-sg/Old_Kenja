<?php
require_once('knja352Model.inc');
require_once('knja352Query.inc');

class knja352Controller extends Controller
{
    public $ModelClassName = "knja352Model";
    public $ProgramID      = "KNJA352";

    public function main()
    {
        $sessionInstance =& Model::getModel($this);
        while (true) {
            switch (trim($sessionInstance->cmd)) {
                //出力
                case "exec1":
                    if (!$sessionInstance->getDownloadModel1()) {
                        $this->callView("knja352Form1");
                    }
                    break 2;
                case "exec2":
                    if (!$sessionInstance->getDownloadModel2()) {
                        $this->callView("knja352Form1");
                    }
                    break 2;
                case "exec3":
                    if (!$sessionInstance->getDownloadModel3()) {
                        $this->callView("knja352Form1");
                    }
                    break 2;
                case "exec4":
                    if (!$sessionInstance->getDownloadModel4()) {
                        $this->callView("knja352Form1");
                    }
                    break 2;
                case "exec5":
                    if (!$sessionInstance->getDownloadModel5()) {
                        $this->callView("knja352Form1");
                    }
                    break 2;
                case "":
                case "main":
                    $sessionInstance->getMainModel();
                    $this->callView("knja352Form1");
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
$knja352Ctl = new knja352Controller;
