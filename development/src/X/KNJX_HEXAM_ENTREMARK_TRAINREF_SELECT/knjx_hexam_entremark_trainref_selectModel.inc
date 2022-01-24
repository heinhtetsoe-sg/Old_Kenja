<?php
class knjx_hexam_entremark_trainref_selectModel extends Model
{
    public $cmd;
    public $schregno;          //学籍番号
    public $year;              //年度

    public function knjx_hexam_entremark_trainref_selectModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->year);
            unsert($this->cmd);

            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::request("SEND_PRGID")) {
            unset($this->cmd);
            $this->getPrgId = VARS::request("SEND_PRGID");
            $this->sendAuth = VARS::request("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->schregno         = VARS::request("SCHREGNO");
        $this->year             = VARS::request("GRADE_YEAR");
        $this->target           = VARS::request("TRAINREF_TARGET");
        $this->keta             = VARS::request("TOTALREMARK_KETA");
        $this->gyo              = VARS::request("TOTALREMARK_GYO");
        $this->torikomiMulti    = VARS::request("TORIKOMI_MULTI");
        $this->shojikouFlg      = VARS::request("SHOJIKOU_FLG");

        $this->getPropertiesAll();

        $this->moji = 15;
        $this->gyou = 66;
    }
}
