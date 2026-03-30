package com.wechat.autoreply.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wechat.autoreply.entity.Tag;
import com.wechat.autoreply.service.TagService;
import com.wechat.autoreply.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/admin/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签管理", description = "标签管理相关接口")
@PreAuthorize("hasRole('ADMIN')")
public class TagController {

    @Resource
    private TagService tagService;

    @GetMapping("/list")
    @Operation(summary = "获取标签列表")
    public Result<List<Tag>> list() {
        List<Tag> tags = tagService.list(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getSortOrder));
        return Result.success(tags);
    }

    @PostMapping
    @Operation(summary = "创建标签")
    public Result<Tag> create(@RequestBody Tag tag) {
        tagService.save(tag);
        return Result.success(tag);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标签")
    public Result<Tag> update(@PathVariable Long id, @RequestBody Tag tag) {
        tag.setId(id);
        tagService.updateById(tag);
        return Result.success(tag);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.removeById(id);
        return Result.success();
    }
}
